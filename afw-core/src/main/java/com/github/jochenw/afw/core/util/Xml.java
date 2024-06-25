package com.github.jochenw.afw.core.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import groovyjarjarpicocli.CommandLine.IParameterConsumer;


/** Utility class for working with XML.
 */
public class Xml {
	/** An objects, which permits writing XML in a convenient manner.
	 */
	public static class Writer {
		/** This class represents an active namespace declaration.
		 */
		public static class NamespaceDeclaration {
			private final String prefix;
			private final String namespaceUri;
			private boolean declared;
			/** Creates a new instance with the given prefix,
			 * and namespace uri.
			 * @param pPrefix The namespace prefix.
			 * @param pNamespaceUri The namespace uri.
			 */
			public NamespaceDeclaration(String pPrefix, String pNamespaceUri) {
				prefix = pPrefix;
				namespaceUri = pNamespaceUri;
			}
			/** Returns the namespace prefix.
			 * @return The namespace prefix.
			 */
			public String getPrefix() { return prefix; }
			/** Returns the namespace URI.
			 * @return The namespace URI.
			 */
			public String getNamespaceUri() { return namespaceUri; }
			/** Returns, whether this namespace declaration has been
			 * propagated.
			 * @return True, if the namespace declaration has been
			 *   propagated. False, if that still needs to be done.
			 */
			public boolean isDeclared() { return declared; }
			/** Called to propagate the namespace declaration by invoking
			 * {@link ContentHandler#startPrefixMapping(String, String)}
			 * on the given content handler. As a side effect, the value
			 * of {@link #isDeclared()} becomes true.
			 * @param pCh The content handler, on which the namespace
			 *   declaration must be propagated.
			 * @throws SAXException Propagation of the namespace
			 *   declaration has failed.
			 */
			public void declare(ContentHandler pCh) throws SAXException {
				pCh.startPrefixMapping(prefix, namespaceUri);
				declared = true;
			}
		}
		private final TransformerHandler transformerHandler;
		private final List<NamespaceDeclaration> activeNamespaces = new ArrayList<>();

		/** Creates a new instance with the given {@link TransformerHandler}.
		 * @param pTransformerHandler The {@link TransformerHandler}, which
		 * is being used.
		 */
		public Writer(TransformerHandler pTransformerHandler) {
			transformerHandler = pTransformerHandler;
		}

		/** Called to declare a namespace.
		 * @param pPrefix The namespace prefix.
		 * @param pNamespaceUri The namespace URI.
		 * @param pConsumer The content creator, which
		 *   creates the content, on which the namespace declaration applies.
		 * @return This {@link Writer}.
		 */
		public Writer namespace(String pPrefix, String pNamespaceUri, Consumer<Writer> pConsumer) {
			final NamespaceDeclaration nsDecl = new NamespaceDeclaration(pPrefix, pNamespaceUri);
			final int index = activeNamespaces.size();
			activeNamespaces.add(nsDecl);
			try {
				nsDecl.declare(transformerHandler);
				accept(pConsumer);
				transformerHandler.endPrefixMapping(pPrefix);
				activeNamespaces.remove(index);
			} catch (SAXException e) {
				throw new UndeclaredThrowableException(e);
			}
			return this;
		}

		/** Invokes the given content creator.
		 * @param pContentCreator The content creator, which is being invoked.
		 */
		protected void accept(Consumer<Writer> pContentCreator) {
			if (pContentCreator != null) {
				pContentCreator.accept(this);
			}
		}

		/** Converts the given {@link Runnable} into a
		 * content creator.
		 * @param pRunnable The {@link Runnable}, which is being
		 *   invoked as a content creator.
		 * @return The converted {@link Runnable}.
		 */
		protected Consumer<Writer> asContentCreator(Runnable pRunnable) {
			if (pRunnable == null) {
				return null;
			}
			return (w) -> pRunnable.run();
		}
		
		/** Called to declare a namespace.
		 * @param pPrefix The namespace prefix.
		 * @param pNamespaceUri The namespace URI.
		 * @param pRunnable The content creator, which
		 *   creates the content, on which the namespace declaration applies.
		 * @return This {@link Writer}.
		 */
		public Writer namespace(String pPrefix, String pNamespaceUri, Runnable pRunnable) {
			return namespace(pPrefix, pNamespaceUri, asContentCreator(pRunnable));
		}

		/** Called to create an element with the given namespace URI, and
		 * local name. The namespace URI is supposed to have been declared.
		 * @param pNamespaceUri The elements namespace Uri. The namespace
		 *   Uri must have been declared in advance by an invocation of
		 *   {@link #namespace(String, String, Consumer)}.
		 * @param pLocalName The elements local name.
		 * @param pContentCreator The elements content creator, if any, or null,
		 *   if the element has no content.
		 * @param pAttributes A set of key/value pairs, which are
		 *   specifying the elements attributes.
		 * @return This {@link Writer}. 
		 */
		public Writer elementNs(String pNamespaceUri, String pLocalName,
				              Consumer<Writer> pContentCreator, String... pAttributes) {
			final String prefix = requirePrefix(pNamespaceUri);
			final String qName = getQName(prefix, pLocalName);
			try {
				final Attributes attrs;
				if (pAttributes != null  &&  pAttributes.length > 0) {
					final AttributesImpl atts = new AttributesImpl();
					for (int i = 0;  i < pAttributes.length;  i += 2) {
						final String name = pAttributes[i];
						final String value = pAttributes[i+1];
						if (value != null) {
							atts.addAttribute(XMLConstants.NULL_NS_URI, name, name,
									          "CDATA", value);
						}
					}
					attrs = atts;
				} else {
					attrs = NO_ATTRIBUTES;
				}
				transformerHandler.startElement(pNamespaceUri, pLocalName, qName, attrs);
				if (pContentCreator != null) {
					pContentCreator.accept(this);
				}
				transformerHandler.endElement(pNamespaceUri, pLocalName, qName);
			} catch (SAXException e) {
				throw new UndeclaredThrowableException(e);
			}
			return this;
		}
	
		/** Called to create an element with the given namespace URI, and
		 * local name. The namespace URI is supposed to have been declared.
		 * @param pNamespaceUri The elements namespace Uri. The namespace
		 *   Uri must have been declared in advance by an invocation of
		 *   {@link #namespace(String, String, Consumer)}.
		 * @param pLocalName The elements local name.
		 * @param pContentCreator The elements content creator, if any, or null,
		 *   if the element has no content.
		 * @param pAttributes A set of key/value pairs, which are
		 *   specifying the elements attributes.
		 * @return This {@link Writer}. 
		 */
		public Writer elementNs(String pNamespaceUri,
				                String pLocalName,
				                Runnable pContentCreator, String... pAttributes) {
			return elementNs(pNamespaceUri, pLocalName,
					       asContentCreator(pContentCreator), pAttributes);
		}

		/** Called to create an element with the given namespace URI, and
		 * local name. The namespace URI is supposed to have been declared.
		 * @param pLocalName The elements local name.
		 * @param pContentCreator The elements content creator, if any, or null,
		 *   if the element has no content.
		 * @param pAttributes A set of key/value pairs, which are
		 *   specifying the elements attributes.
		 * @return This {@link Writer}. 
		 */
		public Writer elementNs(String pLocalName,
				                Runnable pContentCreator, String... pAttributes) {
			return element(pLocalName, asContentCreator(pContentCreator), pAttributes);
		}

		private static final Attributes NO_ATTRIBUTES = new AttributesImpl();

		/** Called to create an element with the current default
		 * namespace URI.
		 * @param pLocalName The elements local name.
		 * @param pContentCreator The elements content creator, if any, or null,
		 *   if the element has no content.
		 * @param pAttributes A set of key/value pairs, which are
		 *   specifying the elements attributes.
		 * @return This {@link Writer}. 
		 */
		public Writer element(String pLocalName,
	                          Consumer<Writer> pContentCreator, String... pAttributes) {
			final String namespaceUri = requireDefaultNamespaceUri();
			return elementNs(namespaceUri, pLocalName, pContentCreator, pAttributes);
		}
		
		/** Called to create an element with the given namespace URI, and
		 * local name. The namespace URI is supposed to have been declared.
		 * @param pNamespaceUri The elements namespace Uri. The namespace
		 *   Uri must have been declared in advance by an invocation of
		 *   {@link #namespace(String, String, Consumer)}.
		 * @param pLocalName The elements local name.
		 * @param pContentCreator The elements content creator, if any, or null,
		 *   if the element has no content.
		 * @param pAttributes A set of key/value pairs, which are
		 *   specifying the elements attributes.
		 * @return This {@link Writer}. 
		 */
		public Writer element(String pNamespaceUri, String pLocalName,
				              Runnable pContentCreator, String... pAttributes) {
			return elementNs(pNamespaceUri, pLocalName, asContentCreator(pContentCreator),
					       pAttributes);
		}

		/** Returns the current default namespace URI.
		 * @return the current default namespace URI, if any.
		 * @throws IllegalStateException No namespace declaration
		 *   is available with the default prefix..
		 */
		protected String requireDefaultNamespaceUri() {
			for (int i = activeNamespaces.size()-1;  i >= 0;  i--) {
				final NamespaceDeclaration nsDecl = activeNamespaces.get(i);
				final String prefix = nsDecl.getPrefix();
				if (prefix == null  ||  prefix.length() == 0) {
					return nsDecl.getNamespaceUri();
				}
			}
			throw new IllegalStateException("There is no namespace declaration"
					+ " with the default prefix.");
		}
		/** Returns an active namespace prefix, which has been declared
		 * for the given namespace URI.
		 * @param pNamespaceUri The namespace Uri, for which a prefix has
		 *   been declared.
		 * @return The requested namespace prefix.
		 * @throws IllegalStateException No namespace declaration is
		 *   available for the given namespace URI.
		 */
		protected String requirePrefix(String pNamespaceUri) {
			for (int i = activeNamespaces.size()-1;  i >= 0;  i--) {
				final NamespaceDeclaration nsDecl = activeNamespaces.get(i);
				if (nsDecl.getNamespaceUri().equals(pNamespaceUri)) {
					return nsDecl.getPrefix();
				}
			}
			throw new IllegalStateException("Undeclared namespace URI: "
			                                + pNamespaceUri); 
		}
		/** Converts the given namespace prefix, and local name,
		 * into a qualified element name.
		 * @param pPrefix The namespace prefix.
		 * @param pLocalName The local name.
		 * @return The resulting qualified name.
		 */
		protected String getQName(String pPrefix, String pLocalName) {
			if (pPrefix == null || pPrefix.length() == 0) {
				return pLocalName;
			} else {
				return pPrefix + ":" + pLocalName;
			}
		}
	}
	/** A class, which represents a generated XML document.
	 * In contrast to the {@link org.w3c.dom.Document W3C DOM Document},
	 * this class is not tied to a particular physical
	 * representation. Instead, it can easily be converted
	 * into a physical representation, like a string,
	 * a byte array, or a {@link org.w3c.dom.Document W3C DOM Document}.
	 */
	public static class Document {
		private final Consumer<Writer> writerConsumer;
		private final Builder builder;

		/** Creates a new instance with the given builder as configuration,
		 * and the given consumer as the content creator.
		 * @param pBuilder A configured builder, which acts as the
		 *   configuration.
		 * @param pWriterConsumer The content creator.
		 */
		public Document(Builder pBuilder, Consumer<Writer> pWriterConsumer) {
			builder = pBuilder;
			writerConsumer = pWriterConsumer;
		}
		/** Converts this document into a {@link org.w3c.dom.Document W3C DOM Document}.
		 * @return The created {@link org.w3c.dom.Document}.
		 */
		public Document getDomDocument() {
			final DOMResult domResult = new DOMResult();
			write(domResult);
			return (Document) domResult.getNode();
		}
		/** Converts the document into a byte array, and returns the array.
		 * @return The created byte array.
		 */
		public byte[] getBytes() {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			write(new StreamResult(baos));
			return baos.toByteArray();
		}
		/** Converts the document into a string, and returns the string.
		 * @return The created string.
		 */
		public String getString() {
			final StringWriter sw = new StringWriter();
			write(new StreamResult(sw));
			return sw.toString();
		}
		/** Converts the document into a {@link Document}, and returns the
		 * document.
		 * @return The created document.
		 */
		public Document getDocument() {
			final DOMResult result = new DOMResult();
			write(result);
			return (Document) result.getNode();
		}
		/** Converts the document by writing it to the given
		 * {@link OutputStream}.
		 * @param pOut The documents target stream.
		 */
		public void write(OutputStream pOut) {
			write(new StreamResult(pOut));
		}
		/** Converts the document by writing it to the given
		 * {@link java.io.Writer}.
		 * @param pOut The documents target stream.
		 */
		public void write(java.io.Writer pOut) {
			write(new StreamResult(pOut));
		}
		/** Converts the document by writing it to the given
		 * {@link Result}.
		 * @param pResult The documents target result.
		 */
		public void write(Result pResult) {
			try {
				final SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();
				final TransformerHandler th = stf.newTransformerHandler();
				final Transformer t = th.getTransformer();
				if (builder.isIndenting()) {
					t.setOutputProperty(OutputKeys.INDENT, "yes");
				}
				if (builder.isOmittingXmlDeclaration()) {
					t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				}
				t.setOutputProperty(OutputKeys.ENCODING, builder.getCharset().name());
				th.setResult(pResult);
				th.startDocument();
				if (writerConsumer != null) {
					final Writer writer = new Writer(th);
					writerConsumer.accept(writer);
				}
				th.endDocument();
			} catch (TransformerException te) {
				throw new UndeclaredThrowableException(te);
			} catch (SAXException se) {
				throw new UndeclaredThrowableException(se);
			}
		}
	}

	/** This exception is being thrown, if an attempt is made to alter
	 * the configuration of an {@link Builder}, which has already been
	 * made immutable by invoking {@link Builder#build(Consumer)}.
	 */
	public static class ImmutableObjectException extends IllegalStateException {
		private static final long serialVersionUID = 4813393381417770060L;

		/** Creates a new instance with the given message, and cause.
		 * @param pMsg The exceptions message.
		 * @param pCause The exceptions cause.
		 */
		public ImmutableObjectException(String pMsg, Throwable pCause) {
			super(pMsg, pCause);
		}

		/** Creates a new instance with the given message, and no cause.
		 * @param pMsg The exceptions message.
		 */
		public ImmutableObjectException(String pMsg) {
			super(pMsg);
		}

		/** Creates a new instance with the given cause, and no message,
		 * @param pCause The exceptions cause.
		 */
		public ImmutableObjectException(Throwable pCause) {
			super(pCause);
		}
	}

	/** This object is used to build the configuration of objects, like
	 * the {@link Writer}, or the {@link Document}.
	 */
	public static class Builder {
		private boolean immutable, indenting, omittingXmlDeclaration;
		private Charset charset = StandardCharsets.UTF_8;
		private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

		/** Creates a new instance with default settings.
		 */
		Builder() {}

		/** Returns, whether this builder is still mutable.
		 * @return True, 
		 * @see #assertMutable()
		 */
		public boolean isMutable() { return !immutable; }

		/** Called to ensure, that this builder is still mutable.
		 * Throws an ImmutableObjectException, if that is not the
		 * case.
		 * @throws ImmutableObjectException The builder is no
		 *   longer mutable.
		 */
		protected void assertMutable() {
			if (immutable) {
				throw new ImmutableObjectException("This builder is no longer mutable.");
			}
		}

		/** Returns, whether the created XML will use indentation.
		 * @return True, if the created XML will use indentation.
		 *   Otherwise false.
		 */
		public boolean isIndenting() { return indenting; }

		/** Sets, whether the created XML will use indentation.
		 * @param pIndenting True, if the created XML will use indentation.
		 *   Otherwise false.
		 * @return This builder.
		 * @throws Xml.ImmutableObjectException The builder is no longer
		 *   mutable.
		 */
		public Builder indenting(boolean pIndenting) {
			assertMutable();
			indenting = pIndenting;
			return this;
		}

		/** Sets, that the created XML will use indentation. This is
		 * equivalent to
		 * <pre>indenting(true)</pre>.
		 * @return This builder.
		 * @throws Xml.ImmutableObjectException The builder is no longer
		 *   mutable.
		 */
		public Builder indenting() {
			return indenting(true);
		}

		/** Returns, whether the created XML will attempt to
		 *   omit an XML declaration.
		 * @return True, if the created XML will attempt to
		 *   omit an XML declaration.
		 */
		public boolean isOmittingXmlDeclaration() { return omittingXmlDeclaration; }

		/** Sets, whether the created XML will attempt to
		 *   omit an XML declaration.
		 * @param pOmittingXmlDeclaration True, if the created XML will attempt to
		 *   omit an XML declaration.
		 * @return This builder.
		 * @throws Xml.ImmutableObjectException The builder is no longer
		 *   mutable.
		 */
		public Builder omittingXmlDeclaration(boolean pOmittingXmlDeclaration) {
			assertMutable();
			omittingXmlDeclaration = pOmittingXmlDeclaration;
			return this;
		}

		/** Sets, that the created XML will attempt to
		 *   omit an XML declaration. This is equivalent to
		 *   <pre>omittingXmlDeclaration(true)</pre>.
		 * @return This builder.
		 * @throws Xml.ImmutableObjectException The builder is no longer
		 *   mutable.
		 */
		public Builder omittingXmlDeclaration() {
			return omittingXmlDeclaration(true);
		}

		/** Returns the character set, which is being used to create the
		 * XML.
		 * @return the character set, which is being used to create the
		 * XML.
		 */
		public Charset getCharset() { return charset; }

		/** Sets the character set, which is being used to create the
		 * XML.
		 * @param pCharset The character set, which is being used to create the
		 * XML. 
		 * @return This builder.
		 * @throws Xml.ImmutableObjectException The builder is no longer
		 *   mutable.
		 * @throws NullPointerException The parameter is null.
		 */
		public Builder charset(Charset pCharset) {
			assertMutable();
			charset = Objects.requireNonNull(pCharset, "Charset");
			return this;
		}

		/** Returns the {@link DateTimeFormatter}, which is being used
		 * for conversion of date/time objects.
		 * @return The {@link DateTimeFormatter}, which is being used
		 * for conversion of date/time objects.
		 */
		public DateTimeFormatter getDateTimeFormatter() { return dateTimeFormatter; }

		/** Sets the {@link DateTimeFormatter}, which is being used
		 * for conversion of date/time objects.
		 * @param pDateTimeFormatter The {@link DateTimeFormatter}, which
		 *   is being used for conversion of date/time objects.
		 * @return This builder.
		 * @throws Xml.ImmutableObjectException The builder is no longer
		 *   mutable.
		 * @throws NullPointerException The parameter is null.
		 */
		public Builder dateTimeFormatter(DateTimeFormatter pDateTimeFormatter) {
			assertMutable();
			dateTimeFormatter = pDateTimeFormatter;
			return this;
		}

		/** Creates an XML document by invoking the given {@code pConsumer},
		 * and returns the document.
		 * @param pWriterConsumer The XML document creator, which is being
		 *   invoked.
		 * @return The created XML document.
		 */
		public Document build(Consumer<Writer> pWriterConsumer) {
			immutable = true;
			return new Document(this, pWriterConsumer);
		}
	}

	/** Creates a new instance of {@link Builder}.
	 * @return The created instance, with default settings.
	 */
	public static Builder builder() { return new Builder(); }
}
