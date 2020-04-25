/**
 * 
 */
package com.github.jochenw.afw.core.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.XMLConstants;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Locator;

/**
 * @author jwi
 *
 */
public class DomHelper {
	/** Exception class, which is thrown by the various assert methods.
	 */
	public static class LocalizableException extends RuntimeException {
		private static final long serialVersionUID = 618692900801230917L;
		private @Nullable final Locator locator;
		/** Creates a new instance with the given error location, the given message, and no cause.
		 * @param pLocator The error location.
		 * @param pMessage The error message.
		 */
		public LocalizableException(@Nullable Locator pLocator, @Nonnull String pMessage) {
			super(pMessage);
			locator = Sax.clone(pLocator);
		}
		/** Creates a new instance with the given error location, the given message, and the given cause.
		 * @param pLocator The error location.
		 * @param pMessage The error message.
		 * @param pCause The error cause.
		 */
		public LocalizableException(@Nullable Locator pLocator, @Nonnull String pMessage, @Nonnull Throwable pCause) {
			super(pMessage, pCause);
			locator = Sax.clone(pLocator);
		}
		/** Creates a new instance with the given error location, no message, and the given cause.
		 * @param pLocator The error location.
		 * @param pCause The error cause.
		 */
		public LocalizableException(@Nullable Locator pLocator, @Nonnull Throwable pCause) {
			super(pCause);
			locator = Sax.clone(pLocator);
		}
		/**
		 * Returns the error location, if available, or null.
		 * @return The error location, if available, or null.
		 */
		public @Nullable Locator getLocator() {
			return locator;
		}
	}
	/** Implementation class for the various getChildren methods.
	 * @param <N> The actual node type.
	 */
	public static class NodeChildrenIterable<N extends Node> implements Iterable<N> {
		private Node parent;
		private final Predicate<Node> predicate;
		/**
		 * Creates a new instance, which iterates over the given nodes children, that match the
		 * given predicate.
		 * @param pParent The parent node, which is being queried for children.
		 * @param pPredicate The predicate, which the children
		 */
		public NodeChildrenIterable(@Nonnull Node pParent, @Nonnull Predicate<Node> pPredicate) {
			predicate = pPredicate;
			parent = pParent;
		}
		@Override
		public Iterator<N> iterator() {
			return new Iterator<N>() {
				private N currentChild;
				private Node nextChild = parent.getFirstChild();

				@Override
				public boolean hasNext() {
					if (currentChild == null) {
						while (nextChild != null) {
							final Node chld = nextChild;
							nextChild = nextChild.getNextSibling();
							if (predicate.test(chld)) {
								@SuppressWarnings("unchecked")
								final N n = (N) chld;
								currentChild = n;
								break;
							}
						}
					}
					return currentChild != null;
				}

				@Override
				public N next() {
					if (currentChild == null) {
						throw new NoSuchElementException();
					}
					final N n = currentChild;
					currentChild = null;
					return n;
				}
			};
		}
	}

	private String defaultNamespaceUri = null;
	private BiFunction<Locator,String,LocalizableException> errorHandler;
	private Function<Node,Locator> locationProvider;

	/**
	 * @return the defaultNamespaceUri
	 */
	public String getDefaultNamespaceUri() {
		return defaultNamespaceUri;
	}

	/**
	 * @param defaultNamespaceUri the defaultNamespaceUri to set
	 */
	public void setDefaultNamespaceUri(String defaultNamespaceUri) {
		this.defaultNamespaceUri = defaultNamespaceUri;
	}

	/**
	 * @return the errorHandler
	 */
	public BiFunction<Locator, String, LocalizableException> getErrorHandler() {
		return errorHandler;
	}

	/**
	 * @param errorHandler the errorHandler to set
	 */
	public void setErrorHandler(BiFunction<Locator, String, LocalizableException> errorHandler) {
		this.errorHandler = errorHandler;
	}
	
	/** Returns the location provider, which is used by the {@link #error(Node,String)}
	 * method.
	 * @return the locationProvider
	 */
	public Function<Node, Locator> getLocationProvider() {
		return locationProvider;
	}

	/**
	 * @param locationProvider the locationProvider to set
	 */
	public void setLocationProvider(Function<Node, Locator> locationProvider) {
		this.locationProvider = locationProvider;
	}

	/**
	 * Returns a {@link LocalizableException localizable exception} with the given nodes
	 * location, and the given error message.
	 * @param pNode The node, which should indicate the errors location.
	 * @param pMessage The error message.
	 * @return A {@link LocalizableException localizable exception} with the given nodes
	 * location, and the given error message.
	 */
	public LocalizableException error(@Nullable Node pNode, @Nonnull String pMessage) {
		final Locator locator;
		if (pNode == null) {
			locator = null;
		} else {
			final Function<Node,Locator> lp = getLocationProvider();
			if (lp == null) {
				locator = null;
			} else {
				locator = lp.apply(pNode);
			}
		}
		final BiFunction<Locator,String,LocalizableException> eh = getErrorHandler();
		if (eh == null) {
			return new LocalizableException(locator, pMessage);
		} else {
			return eh.apply(locator, pMessage);
		}
	}

	/** Returns the given nodes child elements.
	 * @param pNode The parent node, which is being queried for child elements.
	 * @return The parent nodes child elements.
	 */
	public @Nonnull Iterable<Element> getChildren(@Nonnull Node pNode) {
		return new NodeChildrenIterable<Element>(pNode, (n) -> n.getNodeType() == Node.ELEMENT_NODE);
	}

	/** Returns the given nodes child elements with the given local name, and
	 * the default namespace.
	 * @param pNode The parent node, which is being queried for child elements.
	 * @param pLocalName The local name of the requested child elements.
	 * @return The parent nodes child elements with the given local name,
	 *   and the default namespace.
	 */
	public @Nonnull Iterable<Element> getChildren(@Nonnull Node pNode, @Nonnull String pLocalName) {
		return getChildrenNS(pNode, getDefaultNamespaceUri(), pLocalName);
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
	public @Nonnull Iterable<Element> getChildrenNS(@Nonnull Node pNode, @Nullable String pNamespaceUri,
			                                        @Nonnull String pLocalName) {
		return new NodeChildrenIterable<Element>(pNode, (n) -> isElementNS(n, pNamespaceUri, pLocalName));
	}

	/**
	 * Returns, whether the given node is an element with the
	 * {@link #getDefaultNamespaceUri() default namespace URI},
	 * and the given local name.
	 * @param pNode The node, which is being tested.
	 * @param pLocalName The expected local name.
	 * @return True, if the given node is an element with the
	 * {@link #getDefaultNamespaceUri() default namespace URI},
	 * and the given local name.
	 */
	public boolean isElement(@Nonnull Node pNode, @Nonnull String pLocalName) {
		return isElementNS(pNode, getDefaultNamespaceUri(), pLocalName);
	}

	/**
	 * Returns, whether the given node is an element with the given namespace Uri, and the given
	 * local name.
	 * @param pNode The node, which is being tested.
	 * @param pNamespaceUri The expected namespace URI. Null, or
	 *   {@link XMLConstants#NULL_NS_URI} for the default namespace.
	 * @param pLocalName The expected local name.
	 * @return True, if the given node is an element with the given namespace URI, and the given
	 * local name.
	 */
	public boolean isElementNS(@Nonnull Node pNode, @Nullable String pNamespaceUri, @Nonnull String pLocalName) {
		if (pNode.getNodeType() == Node.ELEMENT_NODE) {
			final String namespaceUri = pNode.getNamespaceURI();
			final String localName = pNode.getLocalName();
			if (pNamespaceUri == null  ||  pNamespaceUri.length() == 0) {
				return (namespaceUri == null  ||  namespaceUri.length() == 0)
				    &&  pLocalName.equals(localName);
			} else {
				return pNamespaceUri.equals(namespaceUri)  &&  pLocalName.equals(localName);
			}
		}
		return false;
	}

	/**
	 * Asserts, that the given node is an element. If the assertion fails, throws a
	 * {@link LocalizableException}.
	 * @param pNode The node, which is being tested.
	 * @throws LocalizableException The assertion failed.
	 */
	public void assertElement(@Nonnull Node pNode) throws LocalizableException {
		if (pNode.getNodeType() != Node.ELEMENT_NODE) {
			throw error(pNode, "Expected element, got " + pNode.getClass().getName());
		}
	}

	/**
	 * Asserts, that the given node is an element with the {@link #getDefaultNamespaceUri() default
	 * namespace URI}, and the given local name. If the assertion fails, throws a {@link LocalizableException}.
	 * @param pNode The node, which is being tested.
	 * @param pLocalName The expected local name.
	 * @throws LocalizableException The assertion failed.
	 */
	public void assertElement(@Nonnull Node pNode, @Nonnull String pLocalName) throws LocalizableException {
		assertElementNS(pNode, getDefaultNamespaceUri(), pLocalName);
	}

	/**
	 * Asserts, that the given node is an element with the given
	 * namespace URI, and the given local name. If the assertion
	 * fails, throws a {@link LocalizableException}.
	 * @param pNode The node, which is being tested.
	 * @param pNamespaceURI The expected namespace URI.
	 * @param pLocalName The expected local name.
	 * @throws LocalizableException The assertion failed.
	 */
	public void assertElementNS(@Nonnull Node pNode, @Nonnull String pNamespaceURI, String pLocalName)
	    throws LocalizableException {
		if (pNode.getNodeType() == Node.ELEMENT_NODE) {
			if (!isElementNS(pNode, pNamespaceURI, pLocalName)) {
				throw error(pNode, "Expected " + Sax.asQName(pNamespaceURI, pLocalName)
					        + ", got " + pNode.getNodeName());
			}
		} else {
			throw error(pNode, "Expected element, got " + pNode.getClass().getName());
		}
	}

	/** Returns the first child element of the given node, which has the
	 * {@link #getDefaultNamespaceUri() default namespace URI}, and the
	 * given local name. If no such child element is found, returns null.
	 * @param pNode The requested child's parent node.
	 * @param pLocalName The requested child's local name.
	 * @return The first child element of the given node, which has the
	 * {@link #getDefaultNamespaceUri() default namespace URI}, and the
	 * given local name. Null, if no such child is present.
	 */
	public @Nullable Element getFirstChild(@Nonnull Node pNode, String pLocalName) {
		return getFirstChildNS(pNode, getDefaultNamespaceUri(), pLocalName);
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
	public @Nullable Element getFirstChildNS(@Nonnull Node pNode, String pNamespaceURI, String pLocalName) {
		for (Node node = pNode.getFirstChild();  node != null;  node = node.getNextSibling()) {
			if (isElementNS(node, pNamespaceURI, pLocalName)) {
				return (Element) node;
			}
		}
		return null;
	}

	/** Returns the first child element of the given node, which has the
	 * {@link #getDefaultNamespaceUri() default namespace URI}, and the
	 * given local name. If no such child element is found, returns null.
	 * @param pNode The requested child's parent node.
	 * @param pLocalName The requested child's local name.
	 * @return The first child element of the given node, which has the
	 * {@link #getDefaultNamespaceUri() default namespace URI}, and the
	 * given local name. If no such child element is
	 * found, throws a {@link LocalizableException}.
	 * @throws LocalizableException No matching child is present.
	 */
	public @Nullable Element requireFirstChild(@Nonnull Node pNode, String pLocalName) throws LocalizableException {
		return requireFirstChildNS(pNode, getDefaultNamespaceUri(), pLocalName);
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
	public @Nonnull Element requireFirstChildNS(@Nonnull Node pNode, String pNamespaceURI, String pLocalName)
	    throws LocalizableException {
		final Element e = getFirstChildNS(pNode, pNamespaceURI, pLocalName);
		if (e == null) {
			throw error(pNode, "Expected child element " + Sax.asQName(pNamespaceURI, pLocalName)
			            + ", no such child is present.");
		}
		return e;
	}

	/** Returns the value of the attribute {@code pName} in the element {@code pName},
	 * or null, if no such attribute is present.
	 * @param pNode The element being queried for an attribute value.
	 * @param pName The attribute name.
	 * @return The value of the attribute {@code pName} in the element {@code pName},
	 * or null, if no such attribute is present.
	 * @throws LocalizableException The given node is not an element.
	 */
	public @Nullable String getAttribute(@Nonnull Node pNode, @Nonnull String pName) throws LocalizableException {
		assertElement(pNode);
		final Attr attr = ((Element) pNode).getAttributeNode(pName);
		if (attr == null) {
			return null;
		} else {
			return attr.getValue();
		}
	}

	/** Returns the value of the attribute {@code pName} in the element {@code pName}.
	 * @param pNode The element being queried for an attribute value.
	 * @param pName The attribute name.
	 * @return The value of the attribute {@code pName} in the element {@code pName}.
	 * @throws LocalizableException The given node is not an element, or the element
	 * doesn't have an attribute with the name {@code pName}, or the attribute is
	 * empty.
	 */
	public @Nonnull String requireAttribute(@Nonnull Node pNode, @Nonnull String pName) throws LocalizableException {
		final String value = getAttribute(pNode, pName);
		if (value == null  ||  value.length() == 0) {
			throw error(pNode, "Missing, or empty attribute: " + pNode.getNodeName() + "/@" + pName);
		}
		return value;
	}

	/** Returns the value of the attribute {@code pName} in the element {@code pName}.
	 * @param pNode The element being queried for an attribute value.
	 * @param pName The attribute name.
	 * @param pDefaultValue The attributes default value.
	 * @return The value of the attribute {@code pName} in the element {@code pName}.
	 * @throws LocalizableException The given node is not an element.
	 */
	public @Nonnull String requireAttribute(@Nonnull Node pNode, @Nonnull String pName, @Nonnull String pDefaultValue) throws LocalizableException {
		final String value = getAttribute(pNode, pName);
		if (value == null  ||  value.length() == 0) {
			return pDefaultValue;
		}
		return value;
	}

	/** Returns the boolean value of the attribute {@code pName} in the element {@code pName}.
	 * @param pNode The element being queried for an attribute value.
	 * @param pName The attribute name.
	 * @param pDefaultValue The attributes default value.
	 * @return The boolean value of the attribute {@code pName} in the element {@code pName}.
	 * @throws LocalizableException The given node is not an element.
	 */
	public @Nonnull boolean requireAttribute(@Nonnull Node pNode, @Nonnull String pName, @Nonnull boolean pDefaultValue) throws LocalizableException {
		final String value = getAttribute(pNode, pName);
		if (value == null  ||  value.length() == 0) {
			return pDefaultValue;
		}
		return Boolean.parseBoolean(value);
	}
}
