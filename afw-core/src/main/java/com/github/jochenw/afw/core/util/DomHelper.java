/**
 * 
 */
package com.github.jochenw.afw.core.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import javax.xml.XMLConstants;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import com.github.jochenw.afw.core.function.IStreamableIterable;
import com.google.common.base.Supplier;

/** A helper object for parsing DOM documents.
 */
public class DomHelper {
	/** Creates a new instance.
	 */
	public DomHelper() {}

	/** Exception class, which is thrown by the various assert methods.
	 */
	public static class LocalizableException extends RuntimeException {
		private static final long serialVersionUID = 618692900801230917L;
		/** The errors system id, and public id.
		 */
		private final String systemId, publicId;
		/** The errors line number, and column number.
		 */
		private final int lineNumber, columnNumber;

		/** Creates a new instance with the given error location, the given message, and no cause.
		 * @param pLocator The error location.
		 * @param pMessage The error message.
		 */
		public LocalizableException(@Nullable Locator pLocator, @NonNull String pMessage) {
			super(pMessage);
			if (pLocator == null) {
				systemId = null;
				publicId = null;
				lineNumber = -1;
				columnNumber = -1;
			} else {
				systemId = pLocator.getSystemId();
				publicId = pLocator.getPublicId();
				lineNumber = pLocator.getLineNumber();
				columnNumber = pLocator.getColumnNumber();
			}
		}
		/** Creates a new instance with the given error location, the given message, and the given cause.
		 * @param pLocator The error location.
		 * @param pMessage The error message.
		 * @param pCause The error cause.
		 */
		public LocalizableException(@Nullable Locator pLocator, @NonNull String pMessage, @NonNull Throwable pCause) {
			super(pMessage, pCause);
			if (pLocator == null) {
				systemId = null;
				publicId = null;
				lineNumber = -1;
				columnNumber = -1;
			} else {
				systemId = pLocator.getSystemId();
				publicId = pLocator.getPublicId();
				lineNumber = pLocator.getLineNumber();
				columnNumber = pLocator.getColumnNumber();
			}
		}
		/** Creates a new instance with the given error location, no message, and the given cause.
		 * @param pLocator The error location.
		 * @param pCause The error cause.
		 */
		public LocalizableException(@Nullable Locator pLocator, @NonNull Throwable pCause) {
			super(pCause);
			if (pLocator == null) {
				systemId = null;
				publicId = null;
				lineNumber = -1;
				columnNumber = -1;
			} else {
				systemId = pLocator.getSystemId();
				publicId = pLocator.getPublicId();
				lineNumber = pLocator.getLineNumber();
				columnNumber = pLocator.getColumnNumber();
			}
		}
		/**
		 * Returns the error location, if available, or null.
		 * @return The error location, if available, or null.
		 */
		public @Nullable Locator getLocator() {
			if (systemId == null  &&  publicId == null  &&  lineNumber == -1  &&  columnNumber == -1) {
				return null;
			} else {
				final LocatorImpl locator = new LocatorImpl();
				locator.setColumnNumber(columnNumber);
				locator.setLineNumber(lineNumber);
				locator.setPublicId(publicId);
				locator.setSystemId(systemId);
				return locator;
			}
		}
		/** Returns the error location. Throws a {@link NullPointerException},
		 * if no error location is available.
		 * @return The error location. Never null.
		 */
		public @NonNull Locator requireLocator() {
			final Locator loc = getLocator();
			if (loc == null) {
				throw new NullPointerException("No locator is available.");
			}
			return loc;
		}

		/** Returns the system id of the exceptions location.
		 * @return The system id of the exceptions location.
		 */
		public String getSystemId() {
			return systemId;
		}
		/** Returns the public id of the exceptions location.
		 * @return The public id of the exceptions location.
		 */
		public String getPublicId() {
			return publicId;
		}
		/** Returns the linue number of the exceptions location.
		 * @return The line number of the exceptions location.
		 */
		public int getLineNumber() {
			return lineNumber;
		}
		/** Returns the column number of the exceptions location.
		 * @return The column number of the exceptions location.
		 */
		public int getColumnNumber() {
			return columnNumber;
		}
	}
	/** Implementation class for the various getChildren methods.
	 * @param <N> The actual node type.
	 */
	public static class NodeChildrenIterable<N extends Node> implements IStreamableIterable<N> {
		private @NonNull Node parent;
		private final Predicate<@NonNull Node> predicate;
		/**
		 * Creates a new instance, which iterates over the given nodes children, that match the
		 * given predicate.
		 * @param pParent The parent node, which is being queried for children.
		 * @param pPredicate The predicate, which the children
		 */
		public NodeChildrenIterable(@NonNull Node pParent, @NonNull Predicate<@NonNull Node> pPredicate) {
			predicate = pPredicate;
			parent = pParent;
		}
		@Override
		public Iterator<N> iterator() {
			final List<N> list = new ArrayList<>();
			Node node = parent.getFirstChild();
			while (node != null) {
				if (predicate.test(node)) {
					@SuppressWarnings("unchecked")
					final N nd = (N) node;
					list.add(nd);
				}
				node = node.getNextSibling();
			}
			return list.iterator();
		}
	}

	private String defaultNamespaceUri = null;
	private BiFunction<Locator,@NonNull String,@NonNull LocalizableException> errorHandler;
	private Function<Node,Locator> locationProvider;

	/** Returns the default namespace URI. 
	 * @return the default namespace URI
	 */
	public String getDefaultNamespaceUri() {
		return defaultNamespaceUri;
	}

	/** Sets the default namespace URI. 
	 * @param defaultNamespaceUri The new default namespace URI.
	 */
	public void setDefaultNamespaceUri(String defaultNamespaceUri) {
		this.defaultNamespaceUri = defaultNamespaceUri;
	}

	/** Returns the error handler, which is invoked by {@link #error(Node, String)}.
	 * @return The error handler, which is invoked by {@link #error(Node, String)}.
	 */
	public BiFunction<Locator, @NonNull String, @NonNull LocalizableException> getErrorHandler() {
		return errorHandler;
	}

	/** Sets the error handler, which is invoked by {@link #error(Node, String)}.
	 * @param pErrorHandler The error handler, which is invoked by {@link #error(Node, String)}.
	 */
	public void setErrorHandler(BiFunction<Locator, @NonNull String, @NonNull LocalizableException> pErrorHandler) {
		errorHandler = pErrorHandler;
	}
	
	/** Returns the location provider. May be null, in which case a
	 * default location provider. The default is suitable for
	 * documents, that have been created by the
	 * {@link LocalizableDocument} class.
	 * @return The location provider. May be null, in which case a
	 * default location provider. The default is suitable for
	 * documents, that have been created by the
	 * {@link LocalizableDocument} class.
	 */
	public Function<Node, Locator> getLocationProvider() {
		return locationProvider;
	}

	/** Sets the location provider. May be null, in which case a
	 * default location provider. The default is suitable for
	 * documents, that have been created by the
	 * {@link LocalizableDocument} class.
	 * @param pLocationProvider The newlocation provider. May be
	 * null, in which case a default location provider will be
	 * used. The default is suitable for documents, that have
	 * been created by the {@link LocalizableDocument} class.
	 */
	public void setLocationProvider(Function<Node, Locator> pLocationProvider) {
		locationProvider = pLocationProvider;
	}

	/**
	 * Returns a {@link LocalizableException localizable exception} with the given nodes
	 * location, and the given error message.
	 * @param pNode The node, which should indicate the errors location.
	 * @param pMessage The error message.
	 * @return A {@link LocalizableException localizable exception} with the given nodes
	 * location, and the given error message.
	 */
	public LocalizableException error(@Nullable Node pNode, @NonNull String pMessage) {
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
	public @NonNull IStreamableIterable<Element> getChildren(@NonNull Node pNode) {
		return new NodeChildrenIterable<Element>(pNode, (n) -> n.getNodeType() == Node.ELEMENT_NODE);
	}

	/** Returns the given nodes child elements with the given local name, and
	 * the default namespace.
	 * @param pNode The parent node, which is being queried for child elements.
	 * @param pLocalName The local name of the requested child elements.
	 * @return The parent nodes child elements with the given local name,
	 *   and the default namespace.
	 */
	public @NonNull IStreamableIterable<Element> getChildren(@NonNull Node pNode, @NonNull String pLocalName) {
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
	public @NonNull IStreamableIterable<Element> getChildrenNS(@NonNull Node pNode,
			                                                   @Nullable String pNamespaceUri,
			                                                   @NonNull String pLocalName) {
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
	public boolean isElement(@NonNull Node pNode, @NonNull String pLocalName) {
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
	public boolean isElementNS(@NonNull Node pNode, @Nullable String pNamespaceUri, @NonNull String pLocalName) {
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
	 * @return The input {@code pNode}, casted to an {@link Element}.
	 */
	public Element assertElement(@NonNull Node pNode) throws LocalizableException {
		if (pNode.getNodeType() != Node.ELEMENT_NODE) {
			throw error(pNode, "Expected element, got " + pNode.getClass().getName());
		}
		return (Element) pNode;
	}

	/**
	 * Asserts, that the given node is an element with the {@link #getDefaultNamespaceUri() default
	 * namespace URI}, and the given local name. If the assertion fails, throws a {@link LocalizableException}.
	 * @param pNode The node, which is being tested.
	 * @param pLocalName The expected local name.
	 * @throws LocalizableException The assertion failed.
	 * @return The input {@code pNode}, casted to an {@link Element}.
	 */
	public Element assertElement(@NonNull Node pNode, @NonNull String pLocalName) throws LocalizableException {
		return assertElementNS(pNode, getDefaultNamespaceUri(), pLocalName);
	}

	/**
	 * Asserts, that the given node is an element with the given
	 * namespace URI, and the given local name. If the assertion
	 * fails, throws a {@link LocalizableException}.
	 * @param pNode The node, which is being tested.
	 * @param pNamespaceURI The expected namespace URI, or null for the
	 *   default namespace URI.
	 * @param pLocalName The expected local name.
	 * @throws LocalizableException The assertion failed.
	 * @return The input {@code pNode}, casted to an {@link Element}.
	 */
	public Element assertElementNS(@NonNull Node pNode, @Nullable String pNamespaceURI,
			                       @NonNull String pLocalName)
	    throws LocalizableException {
		if (pNode.getNodeType() == Node.ELEMENT_NODE) {
			if (!isElementNS(pNode, pNamespaceURI, pLocalName)) {
				throw error(pNode, "Expected " + Sax.asQName(pNamespaceURI, pLocalName)
					        + ", got " + pNode.getNodeName());
			}
		} else {
			throw error(pNode, "Expected element, got " + pNode.getClass().getName());
		}
		return (Element) pNode;
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
	public @Nullable Element getFirstChild(@NonNull Node pNode,
			                               @NonNull String pLocalName) {
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
	public @Nullable Element getFirstChildNS(@NonNull Node pNode,
			                                 @Nullable String pNamespaceURI,
			                                 @NonNull String pLocalName) {
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
	public @Nullable Element requireFirstChild(@NonNull Node pNode,
			                                   @NonNull String pLocalName)
	        throws LocalizableException {
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
	public @NonNull Element requireFirstChildNS(@NonNull Node pNode,
			                                    @Nullable String pNamespaceURI,
			                                    @NonNull String pLocalName)
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
	public @Nullable String getAttribute(@NonNull Node pNode, @NonNull String pName) throws LocalizableException {
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
	public @NonNull String requireAttribute(@NonNull Node pNode, @NonNull String pName) throws LocalizableException {
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
	public @NonNull String requireAttribute(@NonNull Node pNode, @NonNull String pName, @NonNull String pDefaultValue) throws LocalizableException {
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
	public boolean requireAttribute(@NonNull Node pNode, @NonNull String pName, boolean pDefaultValue) throws LocalizableException {
		final String value = getAttribute(pNode, pName);
		if (value == null  ||  value.length() == 0) {
			return pDefaultValue;
		}
		return Boolean.parseBoolean(value);
	}

	/** Returns the given attributes value, as a {@link Path} object.
	 * @param pNode The node, which is being queried for an attribute value.
	 * @param pName The requested attributes name.
	 * @return The attributes value, as a {@link Path} object.
	 */
	public @NonNull Path requirePathAttribute(@NonNull Node pNode, @NonNull String pName) {
		final String value = requireAttribute(pNode, pName);
		return Objects.requireNonNull(Paths.get(value));
	}

	/** Returns the given attributes value, as a {@link Path} object.
	 * @param pNode The node, which is being queried for an attribute value.
	 * @param pName The requested attributes name.
	 * @param pPredicate A predicate, which tests the returned value for validity.
	 * @param pErrorMsgSupplier Supplier of the error message, if the returned value is found to be invalid.
	 * @return The attributes value, as a {@link Path} object.
	 */
	public @NonNull Path requirePathAttribute(@NonNull Node pNode, @NonNull String pName, @NonNull Predicate<Path> pPredicate,
			                         @NonNull Supplier<@NonNull String> pErrorMsgSupplier) {
		final Path path = requirePathAttribute(pNode, pName);
		if (pPredicate.test(path)) {
			return path;
		} else {
			throw error(pNode, pErrorMsgSupplier.get());
		}
	}

	/** Returns the content text of the given child element, as a {@link Path} object.
	 * @param pNode The element, which is being queried for a child element.
	 * @param pName The requested elements name.
	 * @param pPredicate A predicate, which tests the returned value for validity.
	 * @param pErrorMsgSupplier Supplier of the error message, if the returned value is found to be invalid.
	 * @return The elements content text, as a {@link Path} object.
	 * @throws LocalizableException No such child element was found, the child element is empty,
	 *   or it's value was found to be invalid by the predicate.
	 * @see #getPathElement(Node, String, Predicate, Function)
	 */
	public Path requirePathElement(@NonNull Node pNode, @NonNull String pName, @NonNull Predicate<Path> pPredicate,
			Function<String,@NonNull String> pErrorMsgSupplier) {
		final Element element = getFirstChild(pNode, pName);
		if (element == null) {
			throw error(pNode, "Expected child element not found: " + pName);
		}
		final String text = element.getTextContent();
		if (text.length() == 0) {
			throw error(element, "Expected non-empty content for element " + element.getLocalName());
		}
		final Path path = Paths.get(text);
		if (pPredicate.test(path)) {
			return path;
		} else {
			throw error(element, pErrorMsgSupplier.apply(path.toString()));
		}
	}

	/** Returns the content text of the given child element, as a {@link Path} object.
	 * @param pNode The element, which is being queried for a child element.
	 * @param pName The requested elements name.
	 * @param pPredicate A predicate, which tests the returned value for validity.
	 * @param pErrorMsgSupplier Supplier of the error message, if the returned value is found to be invalid.
	 * @return The elements content text, as a {@link Path} object, or null, if
	 *   no such child element has been found, the child element was empty, or the
	 *   child elements content failed the predicates test for validity.
	 * @see #requirePathElement(Node, String, Predicate, Function)
	 */
	public Path getPathElement(@NonNull Node pNode, @NonNull String pName, @NonNull Predicate<Path> pPredicate,
			Function<String,@NonNull String> pErrorMsgSupplier) {
		final Element element = getFirstChild(pNode, pName);
		if (element == null) {
			return null;
		}
		final String text = element.getTextContent();
		if (text.length() == 0) {
			throw error(element, "Expected non-empty content for element " + element.getLocalName());
		}
		final Path path = Paths.get(text);
		if (pPredicate.test(path)) {
			return path;
		} else {
			throw error(element, pErrorMsgSupplier.apply(path.toString()));
		}
	}
}
